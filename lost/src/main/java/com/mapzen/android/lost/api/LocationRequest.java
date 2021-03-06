package com.mapzen.android.lost.api;

import com.mapzen.android.lost.internal.PidReader;

import android.os.Parcel;
import android.os.Parcelable;

import static android.os.Process.myPid;

public final class LocationRequest implements Parcelable {
  public static final int PRIORITY_HIGH_ACCURACY = 0x00000064;
  public static final int PRIORITY_BALANCED_POWER_ACCURACY = 0x00000066;
  public static final int PRIORITY_LOW_POWER = 0x00000068;
  public static final int PRIORITY_NO_POWER = 0x00000069;

  static final long DEFAULT_INTERVAL_IN_MS = 3600000;
  static final long DEFAULT_FASTEST_INTERVAL_IN_MS = 600000;
  static final float DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS = 0.0f;

  private long interval = DEFAULT_INTERVAL_IN_MS;
  private long fastestInterval = DEFAULT_FASTEST_INTERVAL_IN_MS;
  private float smallestDisplacement = DEFAULT_SMALLEST_DISPLACEMENT_IN_METERS;
  private int priority = PRIORITY_BALANCED_POWER_ACCURACY;
  private PidReader pidReader = new PidReader() {
    @Override public long getPid() {
      return myPid();
    }
  };
  long pid;

  private LocationRequest() {
    commonInit();
  }

  private LocationRequest(PidReader reader) {
    pidReader = reader;
    commonInit();
  }

  private void commonInit() {
    pid = pidReader.getPid();
  }

  public static LocationRequest create() {
    return new LocationRequest();
  }

  public static LocationRequest create(PidReader reader) {
    return new LocationRequest(reader);
  }

  public LocationRequest(LocationRequest incoming) {
    this.setInterval(incoming.getInterval());
    this.setFastestInterval(incoming.getFastestInterval());
    this.setSmallestDisplacement(incoming.getSmallestDisplacement());
    this.setPriority(incoming.getPriority());
    this.pid = incoming.pid;
  }

  public long getInterval() {
    return interval;
  }

  public LocationRequest setInterval(long millis) {
    interval = millis;

    if (interval < fastestInterval) {
      fastestInterval = interval;
    }

    return this;
  }

  public long getFastestInterval() {
    return fastestInterval;
  }

  public LocationRequest setFastestInterval(long millis) {
    fastestInterval = millis;
    return this;
  }

  public float getSmallestDisplacement() {
    return smallestDisplacement;
  }

  public LocationRequest setSmallestDisplacement(float meters) {
    smallestDisplacement = meters;
    return this;
  }

  public int getPriority() {
    return priority;
  }

  public LocationRequest setPriority(int priority) {
    if (priority != PRIORITY_HIGH_ACCURACY
        && priority != PRIORITY_BALANCED_POWER_ACCURACY
        && priority != PRIORITY_LOW_POWER
        && priority != PRIORITY_NO_POWER) {
      throw new IllegalArgumentException("Invalid priority: " + priority);
    }

    this.priority = priority;
    return this;
  }

  @Override public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    LocationRequest that = (LocationRequest) o;

    if (pid != that.pid) {
      return false;
    }
    if (interval != that.interval) {
      return false;
    }
    if (fastestInterval != that.fastestInterval) {
      return false;
    }
    if (Float.compare(that.smallestDisplacement, smallestDisplacement) != 0) {
      return false;
    }
    return priority == that.priority;
  }

  @Override public int hashCode() {
    int result = (int) (interval ^ (interval >>> 32));
    result = 31 * result + (int) (fastestInterval ^ (fastestInterval >>> 32));
    result =
        31 * result + (smallestDisplacement != +0.0f ? Float.floatToIntBits(smallestDisplacement)
            : 0);
    result = 31 * result + priority;
    result = 31 * result + (int) pid;
    return result;
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(this.interval);
    dest.writeLong(this.fastestInterval);
    dest.writeFloat(this.smallestDisplacement);
    dest.writeInt(this.priority);
    dest.writeLong(this.pid);
  }

  protected LocationRequest(Parcel in) {
    this.interval = in.readLong();
    this.fastestInterval = in.readLong();
    this.smallestDisplacement = in.readFloat();
    this.priority = in.readInt();
    this.pid = in.readLong();
  }

  public static final Parcelable.Creator<LocationRequest> CREATOR =
      new Parcelable.Creator<LocationRequest>() {
        @Override public LocationRequest createFromParcel(Parcel source) {
          return new LocationRequest(source);
        }

        @Override public LocationRequest[] newArray(int size) {
          return new LocationRequest[size];
        }
      };
}
