package com.example.model;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "auteurs")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NamedEntityGraph(name = "Auteur.livres", attributeNodes = @NamedAttributeNode("livres"))
public class Auteur {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nom;
    private String nationalite;
    private LocalDate dateNaissance;
    @Column(nullable = false, unique = true)
    private String email;
    @OneToMany(mappedBy = "auteur", cascade = CascadeType.ALL, orphanRemoval = true)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private List<Livre> livres = new ArrayList<>();

    public Auteur() { }
    public Auteur(String nom, String nationalite, LocalDate dateNaissance, String email) {
        this.nom = nom;
        this.nationalite = nationalite;
        this.dateNaissance = dateNaissance;
        this.email = email;
    }
    public void ajouterLivre(Livre livre) {
        Objects.requireNonNull(livre, "Le livre est obligatoire");
        if (livre.getAuteur() != null && livre.getAuteur() != this) {
            throw new IllegalArgumentException("Le livre appartient deja a un autre auteur");
        }
        if (!livres.contains(livre)) livres.add(livre);
        livre.setAuteur(this);
    }
    public void retirerLivre(Livre livre) {
        if (livres.remove(livre)) livre.setAuteur(null);
    }
    public Long getId() { return id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getNationalite() { return nationalite; }
    public void setNationalite(String nationalite) { this.nationalite = nationalite; }
    public LocalDate getDateNaissance() { return dateNaissance; }
    public void setDateNaissance(LocalDate dateNaissance) { this.dateNaissance = dateNaissance; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public List<Livre> getLivres() { return Collections.unmodifiableList(livres); }
}
