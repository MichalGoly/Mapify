package com.michalgoly.mapify.model;

import com.google.android.gms.maps.model.LatLng;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Unique;
import org.greenrobot.greendao.converter.PropertyConverter;

import java.util.ArrayList;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class LatLngWrapper {

    @Id
    @NotNull
    @Unique
    private Long id = null;

    @NotNull
    private Long polylineWrapperId = null;

    @Convert(converter = LatLngConverter.class, columnType = String.class)
    private LatLng latLng = null;

    public LatLngWrapper() {
        // empty constructor
    }

    @Keep
    public LatLngWrapper(LatLng latLng) {
        this.latLng = latLng;
    }

    @Generated(hash = 1788021726)
    public LatLngWrapper(@NotNull Long id, @NotNull Long polylineWrapperId, LatLng latLng) {
        this.id = id;
        this.polylineWrapperId = polylineWrapperId;
        this.latLng = latLng;
    }

    public static LatLngWrapper toWrapper(LatLng latLng) {
        return new LatLngWrapper(latLng);
    }

    public static LatLng fromWrapper(LatLngWrapper latLngWrapper) {
        return latLngWrapper.getLatLng();
    }

    public static List<LatLngWrapper> toWrapperList(List<LatLng> list) {
        if (list == null)
            return null;
        List<LatLngWrapper> out = new ArrayList<>();
        for (LatLng ll : list)
            out.add(toWrapper(ll));
        return out;
    }

    public static List<LatLng> fromWrapperList(List<LatLngWrapper> list) {
        if (list == null)
            return null;
        List<LatLng> out = new ArrayList<>();
        for (LatLngWrapper llw : list)
            out.add(fromWrapper(llw));
        return out;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public Long getPolylineWrapperId() {
        return this.polylineWrapperId;
    }

    public void setPolylineWrapperId(Long polylineWrapperId) {
        this.polylineWrapperId = polylineWrapperId;
    }

    public static class LatLngConverter implements PropertyConverter<LatLng, String> {

        @Override
        public LatLng convertToEntityProperty(String databaseValue) {
            String[] tokens = databaseValue.split(":");
            return new LatLng(Double.parseDouble(tokens[0]), Double.parseDouble(tokens[1]));
        }

        @Override
        public String convertToDatabaseValue(LatLng entityProperty) {
            return entityProperty.latitude + ":" + entityProperty.longitude;
        }
    }
}
