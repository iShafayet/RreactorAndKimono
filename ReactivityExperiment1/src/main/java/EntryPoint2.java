import rreactor.Kimono;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntryPoint2 {


    public static void main(String[] args) throws InterruptedException {

//        // WORKING
//        Kimono.now("Test")
//                .map(str -> (double) str.length())
//                .map(num -> num + Math.random())
//                .map(str -> str + " Something")
//                .map(str -> str + " is")
//                .map(str -> str + " Right")
//                .sideEffect(str -> {
//                    System.out.println("OUTPUT: " + str);
//                })
//                .run();

//        // WORKING
//        Kimono
//                .now(Math.random())
//                .map(rndNum -> rndNum > 0.5 ? " Good" : " Sad")
//                .sideEffect(str -> {
//                    System.out.println("OUTPUT: " + str);
//                })
//                .run();

//        // WORKING
//        Kimono.now("Life")
//                .map(str -> str + " is")
//                .chain(str -> {
//                    return Kimono
//                            .now(Math.random())
//                            .map(rndNum -> str + (rndNum > 0.5 ? " Good" : " Sad"));
//                })
//                .map(str -> str + ".")
//                .sideEffect(str -> {
//                    System.out.println("OUTPUT: " + str);
//                })
//                .run();

        // WORKING
        Kimono.now("Life")
                .map(str -> str + " is")
                .chain(str -> {
                    return Kimono
                            .now(Math.random())
                            .map(rndNum -> str + (rndNum > 0.5 ? " Good" : " Sad"));
                })
                .map(str -> str + ".")
                .sideEffect(str -> {
                    System.out.println("OUTPUT 1: " + str);
                })
                .map(str -> str + "\n")
                .chain(EntryPoint2::getTitleOfGoogle)
                .map(str -> str + ".")
                .sideEffect(str -> {
                    System.out.println("OUTPUT 1: " + str);
                })
                .run();

        Thread.sleep(5_000);
    }

    static Kimono<String> getTitleOfGoogle(String titleSoFar) {
        var future = new CompletableFuture<String>();
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete("Google");
            executor.shutdown();
        });
        return Kimono.produce(future);
    }


}

