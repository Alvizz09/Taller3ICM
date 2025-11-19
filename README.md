# Taller3ICM 
## Integrantes:
### Juan Carlos Correa
### Sebastián Sanchez
### Juan Sebastián Álvarez
### Angy Bautista

## Cómo funcionan nuestras notificaciones: 
La aplicación tiene un sistema de notificaciones que avisa cuando alguien se pone "Disponible". Básicamente, hay un servicio corriendo en segundo plano (background) que está constantemente escuchando cambios en Firebase. Cuando detecta que un usuario cambió su estado de "No Disponible" a "Disponible", automáticamente mando una notificación local al dispositivo. Se divide ne tres partes: 
### 1. UseravailityService EN ESCUCHACAMBIOS.KT: 
BACKGROUND: significa que algo está corriendo en segundo plano, sin que el usuario vea nada en la pantalla. Esto lo pusimos ya que es esencial para el servicio que escuche cambios 
Este es el servicio de fondo que nunca duerme. Es como un guardia de seguridad que está monitoreando nuestra realtime database de Firebase. Nuestra realtime database se llama users.
Este escucha cuando el usuario cambia su estado, cuando se quita etc. 
¿Por qué usamos START_STICKY?
Porque si Android mata el servicio (por algunos datos basura, memoria etc), automáticamente lo recrea. Ya que lo necesitamos para el servicio en background, así siempre estará corriendo.
### 2. NotificationHelper.KT: 
Esta clase es la que crea y muestra las notificaciones locales en el dispositivo que hicimos.
¿Qué hace?
1. Creamos el canal de notificaciones (necesario desde Android O para que funcione) // segun las diapositivas 
2. Construimos la notificación con:
    Título: "Usuario disponible, checkealo!!"
    Mensaje: "[Nombre] ahora está disponible"
    Un PendingIntent que abre el MainActivity con los datos del usuario
3. Cuando tocas la notificación:
Se abre la app con toda la info del usuario (nombre, ubicación, UID), El UID tiene los datos de lat y long del usuario de la notificacion
Gracias al putExtra() que usamos, la app sabe que venimo de una notificación y puede navegar automáticamente a la pantalla de seguimiento
### 3. MiFirebaseMessaggingService.Kt: 
Es el receptor que comunica con FCM, Este servicio maneja las notificaciones push de Firebase Cloud Messaging (FCM) para notificaciones remotas 



