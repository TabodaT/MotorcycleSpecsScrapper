package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class Model extends MotoListing implements Comparable<Model>{
    private String productionYears;
    public Model(String name, String url, String productionYears) {
        super(name, url);
        this.productionYears = productionYears;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof Model)) return false;

        Model c = (Model) o;

        return this.getName().equals(c.getName()) &&
                this.getUrl().equals(c.getUrl());
    }

    @Override
    public int hashCode() {
        int result = 17;
        if (this.getName() != null) {
            result = 31 * result + this.getName().hashCode();
        }
        if (this.getUrl() != null) {
            result = 31 * result + this.getUrl().hashCode();
        }
        return result;
    }

    @Override
    public int compareTo(Model o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return this.getName() + " ("+ this.getProductionYears() + ") " + this.getUrl();
    }
}
