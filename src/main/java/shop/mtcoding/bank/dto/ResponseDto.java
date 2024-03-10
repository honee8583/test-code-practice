package shop.mtcoding.bank.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ResponseDto<T> {   // 응답 dto는 한번 만들면 수정할일이 없으므로 final 로 설정.
    private final Integer code; // 1: 성공, -1: 실패
    private final String msg;
    private final T data;
}
