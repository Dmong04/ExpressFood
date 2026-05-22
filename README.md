# Comando para la obtención de la huella digital SHA-1, necesaria para la autenticación con Google desde Firebase Console:
keytool -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore"
