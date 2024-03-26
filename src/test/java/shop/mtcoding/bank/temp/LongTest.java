package shop.mtcoding.bank.temp;

import org.junit.jupiter.api.Test;

public class LongTest {

    @Test
    void long_test() {
        // given
        Long number1 = 1111L;
        Long number2 = 1111L;

        Long amount1 = 100L;
        Long amount2 = 1000L;

        // when
        if (number1.longValue() == number2.longValue()) {
            System.out.println("테스트: 동일합니다");
        } else {
            System.out.println("테스트: 동일하지 않습니다");
        }

        // 크고작다를 비교할 때는 longValue()를 사용하지 않아도 된다.
        if (amount1 < amount2) {
            System.out.println("테스트: amount1이 작습니다");
        } else {
            System.out.println("테스트: amount1이 큽니다");
        }

        // then
    }
}
