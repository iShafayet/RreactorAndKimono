import reactor.core.publisher.Mono;

import java.time.Duration;

public class EntryPoint1 {

    private static Mono<String> doSomethingComplexThatReturnsMono() {
        System.out.println("Inside doSomethingComplexThatReturnsMono - start");
        var mono= Mono
                .delay(Duration.ofMillis(2_000L))
                .map(x -> "Test");
        System.out.println("Inside doSomethingComplexThatReturnsMono - end");
        return mono;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("main - start");
//        Mono.just()
        doSomethingComplexThatReturnsMono()
//                .doOnNext(value -> {
//                    System.out.println("value " + value);
//                })
//                .doOnSuccess(value -> {
//                    System.out.println("value " + value);
//                })
                .flatMap(a -> Mono.just(a + " END"))
                .map(a -> a + " END")
                .doOnNext(value -> {
                    System.out.println("value " + value);
                })
                .doOnNext(value -> {
                    System.out.println("value " + value);
                })
                .subscribe();
        System.out.println("main - end");
        Thread.sleep(5_000);
    }

}
