package com.dscjss.judgeapi.submission.model;


import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "compiler")
public class Compiler {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(name = "name")
    private String name;
    @Column(name = "slug")
    private String slug;
    @OneToMany(targetEntity = Version.class, mappedBy = "compiler")
    private List<Version> versions;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
}
