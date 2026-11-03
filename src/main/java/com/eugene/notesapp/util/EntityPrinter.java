package com.eugene.notesapp.util;

import com.eugene.notesapp.model.BaseEntity;
import java.util.Collection;

public final class EntityPrinter {
    private EntityPrinter() { }

    public static void printAll(Collection<? extends BaseEntity> items) {
        for (BaseEntity item : items) {
            System.out.println("id=" + item.getId() + " | " + item.getName());
            System.out.println(item);
        }
    }
}
