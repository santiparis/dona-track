package donante;

import notificacion.EstrategiaDeNotificacion;
import notificacion.NotificacionPorEmail;
import notificacion.NotificacionPorSMS;
import notificacion.NotificacionPorWhatsApp;

public enum TipoContacto {
    WHATSAPP {
        @Override
        public EstrategiaDeNotificacion crearEstrategia() {
            return new NotificacionPorWhatsApp();
        }
    },
    SMS {
        @Override
        public EstrategiaDeNotificacion crearEstrategia() {
            return new NotificacionPorSMS();
        }
    },
    EMAIL {
        @Override
        public EstrategiaDeNotificacion crearEstrategia() {
            return new NotificacionPorEmail();
        }
    };

    public abstract EstrategiaDeNotificacion crearEstrategia();

}
