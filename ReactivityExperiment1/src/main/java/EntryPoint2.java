import rreactor.Kimono;
import rreactor.LogLevel;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
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
                    writeLog("OUTPUT 1: %s", str);
                })
                .map(str -> str + " ")
                .chain(EntryPoint2::getTitleOfGoogle)
                .map(str -> str + ".")
                .sideEffect(str -> {
                    writeLog("OUTPUT 2: %s", str);
                })
                .run();

        Thread.sleep(5_000);
    }

    static Kimono<String> getTitleOfGoogle(String textSoFar) {
        var future = new CompletableFuture<String>();
        writeLog("Created future");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            writeLog("Executing runnable for future");
//            try {
//                Thread.sleep(1_000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            InputStream response = null;
            try {
                String url = "https://www.github.com";
                response = new URL(url).openStream();

                Scanner scanner = new Scanner(response);
                String responseBody = scanner.useDelimiter("\\A").next();
                var title = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));

                future.complete(textSoFar + title);

            } catch (IOException ex) {
                ex.printStackTrace();
            } finally {
                try {
                    assert response != null;
                    response.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                executor.shutdown();
            }
        });
        return Kimono.produce(future);
    }


    private static void writeLog(String pattern, Object... value) {
        var prefix = String.format("t:%s c:%s \t", Thread.currentThread().getName(), "MAIN");
        System.out.println(String.format(prefix + pattern, value));
    }

}

