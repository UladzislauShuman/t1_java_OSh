package ru.t1.java.demo.service.impl;

import java.io.IOException;
import java.util.List;

public interface ImplementService<Type, TypeDto> {
    List<Type> parseJson() throws IOException;
}
