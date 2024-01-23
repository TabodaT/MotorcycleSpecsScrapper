package com.example.scrapping.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ModelOfManuf extends MotoListing implements Comparable<ModelOfManuf>{
    private String productionYears;
    private boolean inserted = false;
    private int page;

    public ModelOfManuf(String name, String url, String productionYears, int page) {
        super(name, url);
        this.productionYears = productionYears;
        this.page = page;
    }

    public ModelOfManuf(ModelOfManuf model){
        super(model.getName(),model.getUrl());
        this.productionYears = model.getProductionYears();
        this.page = model.getPage();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;

        if (!(o instanceof ModelOfManuf)) return false;

        ModelOfManuf c = (ModelOfManuf) o;

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
    public int compareTo(ModelOfManuf o) {
        return this.toString().compareTo(o.toString());
    }

    @Override
    public String toString() {
        return this.getName() + " ("+ this.getProductionYears() + ") " + this.getUrl();
    }
}
