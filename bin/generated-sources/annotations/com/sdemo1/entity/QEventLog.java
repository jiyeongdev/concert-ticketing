package com.sdemo1.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QEventLog is a Querydsl query type for EventLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QEventLog extends EntityPathBase<EventLog> {

    private static final long serialVersionUID = -1897080497L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEventLog eventLog = new QEventLog("eventLog");

    public final DateTimePath<java.sql.Timestamp> createdAt = createDateTime("createdAt", java.sql.Timestamp.class);

    public final StringPath details = createString("details");

    public final StringPath eventType = createString("eventType");

    public final NumberPath<java.math.BigInteger> id = createNumber("id", java.math.BigInteger.class);

    public final QMember user;

    public QEventLog(String variable) {
        this(EventLog.class, forVariable(variable), INITS);
    }

    public QEventLog(Path<? extends EventLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QEventLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QEventLog(PathMetadata metadata, PathInits inits) {
        this(EventLog.class, metadata, inits);
    }

    public QEventLog(Class<? extends EventLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QMember(forProperty("user")) : null;
    }

}

