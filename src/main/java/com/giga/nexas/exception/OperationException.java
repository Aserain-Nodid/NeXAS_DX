package com.giga.nexas.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OperationException extends RuntimeException {
    private Integer code;
    private String msg;

    public OperationException(Integer code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }
}
