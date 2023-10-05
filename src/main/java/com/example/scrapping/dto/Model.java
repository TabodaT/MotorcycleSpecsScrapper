package com.example.scrapping.dto;

import java.util.List;

public class Model extends MotoListing implements Comparable<Model>{
    public Model(String name, String url) {
        super(name, url);
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
}
