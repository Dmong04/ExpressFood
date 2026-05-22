# Comando para la generación de la huella digital SHA-1, para la autenticación con Google desde Firebase Console:
keytool -genkey -v -keystore "$env:USERPROFILE\.android\debug.keystore" `
>>   -storepass android -alias androiddebugkey -keypass android `
>>   -keyalg RSA -keysize 2048 -validity 10000 `
>>   -dname "CN=Android Debug, O=Android, C=US"
Generating 2048-bit RSA key pair and self-signed certificate (SHA384withRSA) with a validity of 10 000 days
        for: CN=Android Debug, O=Android, C=US

# Comando para la obtención de la huella digital SHA-1, necesaria para la autenticación con Google desde Firebase Console:
keytool -list -v -alias androiddebugkey -keystore "$env:USERPROFILE\.android\debug.keystore"
