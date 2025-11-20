package com.upc.confin;

public class User {
    private String id;
    private String nombre;      // Nombre completo
    private String username;    // Usuario único
    private String email;       // Email (nuevo)
    private String password;    // Hash encriptado
    private String photoUrl;    // Foto de perfil (para Google)

    // Constructor vacío para Firebase
    public User() {
    }

    // Constructor completo
    public User(String id, String nombre, String username, String email, String password, String photoUrl) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.email = email;
        this.password = password;
        this.photoUrl = photoUrl;
    }

    // Constructor sin photoUrl (para registro manual)
    public User(String id, String nombre, String username, String email, String password) {
        this.id = id;
        this.nombre = nombre;
        this.username = username;
        this.email = email;
        this.password = password;
        this.photoUrl = null;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}