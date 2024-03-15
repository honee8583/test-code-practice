SET REFERENTIAL_INTEGRITY FALSE;    -- 제약 조건 비활성화
truncate table transaction_tb;  -- create문을 실행할 필요가 없다. 내용만 제거한다. (컨트롤러에서는 이를 사용)
truncate table account_tb;
truncate table user_tb;
SET REFERENTIAL_INTEGRITY TRUE;     -- 제약 조건 활성화