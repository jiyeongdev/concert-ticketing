package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeatHold is a Querydsl query type for SeatHold
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeatHold extends EntityPathBase<SeatHold> {

    private static final long serialVersionUID = -1285485719L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeatHold seatHold = new QSeatHold("seatHold");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final DateTimePath<java.time.LocalDateTime> holdExpireAt = createDateTime("holdExpireAt", java.time.LocalDateTime.class);

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final QSeat seat;

    public final QMember user;

    public QSeatHold(String variable) {
        this(SeatHold.class, forVariable(variable), INITS);
    }

    public QSeatHold(Path<? extends SeatHold> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeatHold(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeatHold(PathMetadata metadata, PathInits inits) {
        this(SeatHold.class, metadata, inits);
    }

    public QSeatHold(Class<? extends SeatHold> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.seat = inits.isInitialized("seat") ? new QSeat(forProperty("seat"), inits.get("seat")) : null;
        this.user = inits.isInitialized("user") ? new QMember(forProperty("user")) : null;
    }

}

