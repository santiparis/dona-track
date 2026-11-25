// Registrar donación: los bienes se cargan de a uno en el recuadro "bien" y se suman a la tabla.
// Cada fila lleva sus datos en inputs ocultos (bienes[0].cantidad, ...): el submit del form manda todos juntos.
const bienes = [];
const datos = ["subcategoria", "cantidad", "descripcion", "estado", "vencimiento"];
let editando = null; // posición del bien que se está editando; null si se carga uno nuevo

const campo = id => document.getElementById(id);
// Evita que un texto con comillas o "<" rompa el HTML de la tabla
const escapar = texto => String(texto).replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/"/g, "&quot;");

function abrir(titulo) {
    campo("bien-titulo").textContent = titulo;
    campo("agregar-bien").textContent = editando === null ? "Agregar a la lista" : "Guardar cambios";
    campo("bien").hidden = false;
}

function cerrar() {
    datos.forEach(dato => campo("bien-" + dato).value = "");
    campo("bien-foto").value = "";
    campo("bien-error").hidden = true;
    campo("bien").hidden = true;
    editando = null;
    mostrarUnidad();
}

function validar(bien, opcion) {
    if (!bien.subcategoria) return "Elegí una subcategoría.";
    if (!(bien.cantidad > 0)) return "Ingresá una cantidad mayor a 0.";
    if (opcion.dataset.estado && !bien.estado) return "Elegí si es nuevo o usado.";
    if (opcion.dataset.vence && !bien.vencimiento) return "Este bien es perecedero: ingresá el vencimiento.";
    if (bien.foto && !["image/jpeg", "image/png"].includes(bien.foto.type)) return "La foto debe ser JPG o PNG.";
    if (bien.foto && bien.foto.size > 5 * 1024 * 1024) return "La foto puede pesar hasta 5 MB.";
    return "";
}

function agregarBien() {
    const opcion = campo("bien-subcategoria").selectedOptions[0];
    const bien = { nombre: opcion.textContent };
    datos.forEach(dato => bien[dato] = campo("bien-" + dato).value);
    const fotoNueva = campo("bien-foto").files[0];
    bien.foto = fotoNueva || (editando === null ? null : bienes[editando].foto);

    const error = validar(bien, opcion);
    campo("bien-error").textContent = error;
    campo("bien-error").hidden = !error;
    if (error) return;

    bien.unidad = opcion.dataset.unidad;
    if (!opcion.dataset.estado) bien.estado = "";
    if (!opcion.dataset.vence) bien.vencimiento = "";
    if (editando === null) bienes.push(bien);
    else bienes[editando] = bien;
    cerrar();
    dibujarTabla();
}

function editarBien(posicion) {
    editando = posicion;
    datos.forEach(dato => campo("bien-" + dato).value = bienes[posicion][dato]);
    mostrarUnidad();
    abrir("Editar bien " + (posicion + 1));
}

function quitarBien(posicion) {
    bienes.splice(posicion, 1);
    dibujarTabla();
}

// Una fila por bien: lo que se ve y, en la última celda, los inputs ocultos que viajan en el submit
function fila(bien, posicion) {
    const ocultos = datos.map(dato => `<input type="hidden" name="bienes[${posicion}].${dato}" value="${escapar(bien[dato])}">`);
    const detalle = [bien.estado, bien.vencimiento, bien.descripcion].filter(Boolean).join(" · ") || "—";
    const nombreFoto = bien.foto ? escapar(bien.foto.name) : "—";
    return `<tr>
        <td>${escapar(bien.nombre)}</td>
        <td>${escapar(bien.cantidad + " " + bien.unidad)}</td>
        <td>${escapar(detalle)}</td>
        <td>${nombreFoto}<input id="foto-bien-${posicion}" type="file" name="bienes[${posicion}].foto" accept="image/jpeg,image/png" hidden></td>
        <td>
            <div class="acciones-tabla">
                <button class="btn2 chico" type="button" onclick="editarBien(${posicion})">Editar</button>
                <button class="btn2 chico peligro" type="button" onclick="quitarBien(${posicion})">Quitar</button>
            </div>
            ${ocultos.join("")}
        </td>
    </tr>`;
}

function dibujarTabla() {
    campo("bienes").innerHTML = bienes.map(fila).join("");
    bienes.forEach((bien, posicion) => {
        if (!bien.foto) return;
        const transferencia = new DataTransfer();
        transferencia.items.add(bien.foto);
        campo("foto-bien-" + posicion).files = transferencia.files;
    });
    campo("bienes-titulo").textContent = "2. Bienes (" + bienes.length + ")";
    campo("sin-bienes").hidden = bienes.length > 0;
    campo("registrar").disabled = bienes.length === 0;
}

campo("nuevo-bien").onclick = () => { cerrar(); abrir("Nuevo bien"); };
campo("agregar-bien").onclick = agregarBien;
campo("cancelar-bien").onclick = cerrar;
// Enter dentro del recuadro agrega el bien en vez de mandar el form entero
campo("bien").onkeydown = evento => {
    if (evento.key === "Enter") {
        evento.preventDefault();
        agregarBien();
    }
};

// La unidad la define la subcategoría: se muestra al lado de la cantidad
function mostrarUnidad() {
    campo("bien-unidad").value = campo("bien-subcategoria").selectedOptions[0].dataset.unidad || "";
}

campo("bien-subcategoria").onchange = mostrarUnidad;
