package com.michalgoly.mapify.model;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.ToMany;
import org.greenrobot.greendao.annotation.ToOne;
import org.greenrobot.greendao.annotation.Unique;

import java.util.Date;
import java.util.List;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.DaoException;

@Entity
public class PolylineWrapper {

    @Id(autoincrement = true)
    @Unique
    private Long id = null;

    @ToMany(referencedJoinProperty = "polylineWrapperId")
    private List<LatLngWrapper> points = null;
    private int color = -1;
    private Date startDate = null;
    private Date endDate = null;

    @ToOne
    private TrackWrapper trackWrapper = null;

    @ToOne
    private User user = null;

    /** Used to resolve relations */
    @Generated(hash = 2040040024)
    private transient DaoSession daoSession;

    /** Used for active entity operations. */
    @Generated(hash = 1723657017)
    private transient PolylineWrapperDao myDao;

    @Generated(hash = 1220415639)
    private transient boolean trackWrapper__refreshed;

    @Generated(hash = 2122020312)
    private transient boolean user__refreshed;

    public PolylineWrapper() {
        // empty constructor
    }

    @Keep
    public PolylineWrapper(List<LatLngWrapper> points, int color, TrackWrapper trackWrapper, Date startDate,
                           Date endDate, User user) {
        this.points = points;
        this.color = color;
        this.trackWrapper = trackWrapper;
        this.startDate = startDate;
        this.endDate = endDate;
        this.user = user;
    }

    @Generated(hash = 224276568)
    public PolylineWrapper(Long id, int color, Date startDate, Date endDate) {
        this.id = id;
        this.color = color;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    @Keep
    public List<LatLngWrapper> getPoints() {
        return points;
    }

    @Keep
    public void setPoints(List<LatLngWrapper> points) {
        this.points = points;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Keep
    public TrackWrapper getTrackWrapper() {
        return trackWrapper;
    }

    @Keep
    public void setTrackWrapper(TrackWrapper trackWrapper) {
        this.trackWrapper = trackWrapper;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Keep
    public User getUser() {
        return user;
    }

    @Keep
    public void setUser(User user) {
        this.user = user;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 1140114504)
    public TrackWrapper peakTrackWrapper() {
        return trackWrapper;
    }

    /** To-one relationship, returned entity is not refreshed and may carry only the PK property. */
    @Generated(hash = 1689829570)
    public User peakUser() {
        return user;
    }

    /** Resets a to-many relationship, making the next get call to query for a fresh result. */
    @Generated(hash = 1076404248)
    public synchronized void resetPoints() {
        points = null;
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#delete(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 128553479)
    public void delete() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.delete(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#refresh(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 1942392019)
    public void refresh() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.refresh(this);
    }

    /**
     * Convenient call for {@link org.greenrobot.greendao.AbstractDao#update(Object)}.
     * Entity must attached to an entity context.
     */
    @Generated(hash = 713229351)
    public void update() {
        if (myDao == null) {
            throw new DaoException("Entity is detached from DAO context");
        }
        myDao.update(this);
    }

    /** called by internal mechanisms, do not call yourself. */
    @Generated(hash = 1837455768)
    public void __setDaoSession(DaoSession daoSession) {
        this.daoSession = daoSession;
        myDao = daoSession != null ? daoSession.getPolylineWrapperDao() : null;
    }

}
