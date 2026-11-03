package com.eugene.notesapp.orm.entity;

import org.hibernate.annotations.IdGeneratorType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@IdGeneratorType(CourseIdGenerator.class)
@Retention(RUNTIME)
@Target(FIELD)
public @interface CourseGeneratedId { }
