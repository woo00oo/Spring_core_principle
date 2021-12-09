package hello.advanced.trace.threadlocal;

import hello.advanced.trace.threadlocal.code.FieldService;
import hello.advanced.trace.threadlocal.code.ThreadLocalService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class ThreadLocalServiceTest {

    private ThreadLocalService service = new ThreadLocalService();

    @Test
    void filed() {
        log.info("main start");

        Thread threadA = new Thread(() -> service.logic("userA"));
        threadA.setName("thread-A");
        Thread threadB = new Thread(() -> service.logic("userB"));
        threadB.setName("thread-B");

        threadA.start();
        //sleep(2000); //동시성 문제 발생X
        sleep(100); //동시성 문제 발생O
        threadB.start();

        sleep(3000); //메인 쓰레드 종료 대기
        log.info("main exit");
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
