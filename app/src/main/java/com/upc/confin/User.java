package com.upc.confin; // Reemplaza con tu paquete

/**
 * Clase Modelo (POJO) para representar la información del usuario
 * que se guardará en Firebase Realtime Database.
 */
public class User {
    // Los campos deben ser públicos o tener getters/setters para Firebase.
    public String nombre;
    public String email;

    /**
     * Constructor vacío.
     * ¡OBLIGATORIO para que Firebase Realtime Database pueda leer los datos!
     */
    public User() {
    }

    /**
     * Constructor para crear un nuevo objeto de usuario.
     * @param nombre Nombre completo del usuario.
     * @param email Correo electrónico del usuario.
     */
    public User(String nombre, String email) {
        this.nombre = nombre;
        this.email = email;
    }
}