package donaciones.storage;

import io.javalin.http.UploadedFile;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/** Acceso de servidor a un contenedor privado de Azure Blob Storage. */
public class FotosRecepcion {
  public static final int MAX_BYTES = 5 * 1024 * 1024;
  private final String contenedor;
  private final String sas;
  private final HttpClient cliente = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  public FotosRecepcion(String contenedor, String sas) {
    this.contenedor = contenedor == null ? "" : contenedor.strip().replaceAll("/+$", "");
    this.sas = sas == null ? "" : sas.strip().replaceFirst("^\\?", "");
    if (!this.contenedor.isEmpty()) {
      URI uri = URI.create(this.contenedor);
      boolean local = "http".equals(uri.getScheme()) && "127.0.0.1".equals(uri.getHost());
      if ((!local && !"https".equals(uri.getScheme())) || uri.getHost() == null
          || uri.getRawQuery() != null || uri.getFragment() != null || uri.getUserInfo() != null
          || uri.getPath().isBlank()) {
        throw new IllegalArgumentException("Configurá la URL del contenedor de fotos sin credenciales.");
      }
    }
  }

  public boolean configurado() {
    return !contenedor.isBlank() && !sas.isBlank();
  }

  public List<Foto> validar(List<UploadedFile> archivos) {
    if (archivos.isEmpty() || archivos.size() > 3) {
      throw new IllegalArgumentException("Adjuntá entre una y tres fotos.");
    }
    return archivos.stream().map(this::validarFoto).toList();
  }

  private Foto validarFoto(UploadedFile archivo) {
    try (var contenido = archivo.content()) {
      byte[] bytes = contenido.readNBytes(MAX_BYTES + 1);
      if (bytes.length > MAX_BYTES) throw new IllegalArgumentException("Cada foto admite hasta 5 MB.");
      try (var imagen = new MemoryCacheImageInputStream(new ByteArrayInputStream(bytes))) {
        var lectores = ImageIO.getImageReaders(imagen);
        if (lectores.hasNext()) {
          var lector = lectores.next();
          try {
            lector.setInput(imagen, true, true);
            String formato = lector.getFormatName().toLowerCase(Locale.ROOT);
            if (List.of("png", "jpeg").contains(formato)
                && (long) lector.getWidth(0) * lector.getHeight(0) <= 16_000_000) {
              lector.read(0);
              return new Foto(UUID.randomUUID().toString(), "image/" + formato, bytes);
            }
          } finally {
            lector.dispose();
          }
        }
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("No se pudo leer una de las fotos.");
    }
    throw new IllegalArgumentException("Usá fotos JPG o PNG de hasta 16 megapíxeles.");
  }

  public void guardar(Foto foto) {
    var respuesta = enviar(pedido(foto.id())
        .header("x-ms-blob-type", "BlockBlob")
        .header("Content-Type", foto.tipo())
        .header("If-None-Match", "*")
        .PUT(HttpRequest.BodyPublishers.ofByteArray(foto.bytes())).build(),
        HttpResponse.BodyHandlers.discarding());
    if (respuesta.statusCode() != 201) throw new IllegalStateException("No se pudo guardar la foto.");
  }

  public byte[] leer(String id) {
    var respuesta = enviar(pedido(id).GET().build(), HttpResponse.BodyHandlers.ofInputStream());
    try (var contenido = respuesta.body()) {
      if (respuesta.statusCode() == 404) return null;
      if (respuesta.statusCode() != 200) throw new IllegalStateException("No se pudo leer la foto.");
      byte[] bytes = contenido.readNBytes(MAX_BYTES + 1);
      if (bytes.length > MAX_BYTES) throw new IllegalStateException("La foto supera el tamaño permitido.");
      return bytes;
    } catch (IOException e) {
      throw new IllegalStateException("No se pudo leer la foto.");
    }
  }

  public void borrar(String id) {
    var respuesta = enviar(pedido(id).DELETE().build(), HttpResponse.BodyHandlers.discarding());
    if (respuesta.statusCode() != 202 && respuesta.statusCode() != 404) {
      throw new IllegalStateException("No se pudo retirar una foto de una confirmación fallida.");
    }
  }

  private HttpRequest.Builder pedido(String id) {
    if (!configurado()) throw new IllegalStateException("El almacenamiento de fotos no está configurado.");
    return HttpRequest.newBuilder(URI.create(contenedor + "/" + UUID.fromString(id) + "?" + sas))
        .timeout(Duration.ofSeconds(15)).header("x-ms-version", "2023-11-03");
  }

  private <T> HttpResponse<T> enviar(HttpRequest pedido, HttpResponse.BodyHandler<T> handler) {
    try {
      return cliente.send(pedido, handler);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Se interrumpió el acceso a las fotos.");
    } catch (IOException e) {
      // El detalle de la excepción puede contener la URL con el SAS.
      throw new IllegalStateException("El almacenamiento de fotos no respondió.");
    }
  }

  public record Foto(String id, String tipo, byte[] bytes) {}
}
