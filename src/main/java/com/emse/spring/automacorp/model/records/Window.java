package com.emse.spring.automacorp.model.records;

import com.emse.spring.automacorp.model.WindowStatus;

public record Window(Long id, String name, WindowStatus windowStatus, Long roomId) {
}
