package io.botmaker.simpleredis.property;

import io.botmaker.simpleredis.model.RedisEntity;
import io.botmaker.simpleredis.util.TimeUtils;

import java.io.Serializable;
import java.util.Date;

public class DateProperty extends StringProperty implements Serializable {

    private static final long serialVersionUID = 4859456196836703354L;

    public DateProperty(final RedisEntity owner) {
        super(owner);
    }

    /**
     *
     * @return the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by this date.
     */
    public long getTicks() {
        final String isoDate = get();
        if (isoDate == null || isoDate.trim().length() == 0) {
            return 0;
        }
        return TimeUtils.fromISODate(isoDate).getTime();
    }

    /**
     *
     * @param ticks the number of milliseconds since January 1, 1970, 00:00:00 GMT
     *          represented by this date.
     */
    public void setTicks(final long ticks) {
        set(TimeUtils.toISODate(new Date(ticks)));
    }
}
