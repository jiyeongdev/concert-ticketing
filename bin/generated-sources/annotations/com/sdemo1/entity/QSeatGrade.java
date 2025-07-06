package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QSeatGrade is a Querydsl query type for SeatGrade
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QSeatGrade extends EntityPathBase<SeatGrade> {

    private static final long serialVersionUID = -1196196243L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QSeatGrade seatGrade = new QSeatGrade("seatGrade");

    public final QConcert concert;

    public final StringPath gradeName = createString("gradeName");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public QSeatGrade(String variable) {
        this(SeatGrade.class, forVariable(variable), INITS);
    }

    public QSeatGrade(Path<? extends SeatGrade> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QSeatGrade(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QSeatGrade(PathMetadata metadata, PathInits inits) {
        this(SeatGrade.class, metadata, inits);
    }

    public QSeatGrade(Class<? extends SeatGrade> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.concert = inits.isInitialized("concert") ? new QConcert(forProperty("concert")) : null;
    }

}

