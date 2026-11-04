package com.eugene.notesapp.orm.entity;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import java.util.EnumSet;
import static org.hibernate.generator.EventTypeSets.INSERT_ONLY;

/** Keeps LR7 assigned ids and LR10's unchanged schema; also generates ids for new ORM graphs. */
public final class CourseIdGenerator implements BeforeExecutionGenerator {
    @Override
    public Object generate(SharedSessionContractImplementor session, Object owner,
                           Object currentValue, EventType eventType) {
        return currentValue != null ? currentValue : com.eugene.notesapp.model.BaseEntity.nextId();
    }

    @Override
    public boolean allowAssignedIdentifiers() { return true; }

    @Override
    public EnumSet<EventType> getEventTypes() { return INSERT_ONLY; }
}
