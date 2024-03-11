package shop.mtcoding.bank.temp;

import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

// java.util.regex.Pattern
public class RegexTest {

    @Test
    void 한글만된다_test() throws Exception {
        String value = "한글";
        boolean result = Pattern.matches("^[가-힣]+$", value);
        System.out.println("테스트: " + result);
    }

    @Test
    void 한글은안된다_test() throws Exception {
        String value = "ㅇ";
        boolean result = Pattern.matches("^[^ㄱ-ㅎ가-힣]*$", value);
        System.out.println("테스트: " + result);
    }

    @Test
    void 영어만된다_test() throws Exception {
        String value = "ssar";
        boolean result = Pattern.matches("^[a-zA-Z]+$", value);
        System.out.println("테스트: " + result);
    }

    @Test
    void 영어는안된다_test() throws Exception {
        String value = "가나다abc";
        boolean result = Pattern.matches("^[^a-zA-Z]*$", value);
        System.out.println("한글과 영어가 들어갈경우 false가 되어야 한다 => " + result);

        value = "abc";
        result = Pattern.matches("^[^a-zA-Z]*$", value);
        System.out.println("영어만 들어갈경우 false가 되어야 한다 => " + result);

        value = "가나다123";
        result = Pattern.matches("^[^a-zA-Z]*$", value);
        System.out.println("영어가 들어가지 않을 경우 true가 되어야 한다 => " + result);

        value = "";
        result = Pattern.matches("^[^a-zA-Z]*$", value);
        System.out.println("공백일 경우 true가 되어야 한다 => " + result);
    }

    @Test
    void 영어와숫자만된다_test() throws Exception {
        String value = "ssar1234";
        boolean result = Pattern.matches("^[a-zA-Z0-9]+$", value);
        System.out.println("영어와 숫자가 들어갈 경우 true가 되어야한다 => " + result);

        value = "가나다ssar";
        result = Pattern.matches("^[a-zA-Z0-9]+$", value);
        System.out.println("한글이 들어갈 경우 false가 되어야한다 => " + result);
    }

    @Test
    void 영어만되고_길이는최소2최대4이다_test() throws Exception {
        String value = "ssar";
        boolean result = Pattern.matches("^[a-zA-Z]{2,4}$", value);
        System.out.println("영어가 2~4자리일 경우 true가 되어야 한다 => " + result);
    }

    @Test
    void user_username_test() {
        String username = "ssar123";
        boolean result = Pattern.matches("^[a-zA-Z0-9]{2,20}$", username);
        System.out.println("테스트: " + result);
    }

    @Test
    void user_fullname_test() {
        // 영어, 한글 1~20자
        String fullname = "ssar";
        boolean result = Pattern.matches("^[a-zA-Z가-힣]{1,20}$", fullname);
        System.out.println("테스트: " + result);
    }

    @Test
    void user_email_test() {
        // 이메일 형식
        String email = "ssar@nate.com";
        boolean result = Pattern.matches("^[a-zA-Z0-9]{2,6}@[a-zA-Z0-9]{2,6}\\.[a-zA-Z]{2,3}$", email);
        System.out.println("테스트: " + result);
    }
}
