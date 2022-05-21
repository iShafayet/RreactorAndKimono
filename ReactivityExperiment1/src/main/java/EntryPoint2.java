import rreactor.Kimono;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EntryPoint2 {

    public static void main(String[] args) throws InterruptedException {
        // THIS CODE WORKS
        Kimono.now("Life")
                .map(str -> str + " is")
                .map(str -> {
                    throw new RuntimeException("Example");
                })
                .onError(str -> {
                    writeLog("", str);
                })
                .chain(str -> {
                    return Kimono
                            .now(Math.random())
                            .map(rndNum -> str + (rndNum > 0.5 ? " good" : " sad"));
                })
                .sideEffect(str -> {
                    writeLog("OUTPUT 1: %s", str);
                })
                .map(str -> str + " according to ")
                .chain(str -> {
                    return Kimono
                            .now(str)
                            .chain(EntryPoint2::getTitleOfAPageFake)
                            .map(str1 -> str + toTitleCase(str1));
                })
                .map(str -> str + ".")
                .sideEffect(str -> {
                    writeLog("OUTPUT 2: %s", str);
                })
                .run();

        Thread.sleep(5_000);
    }

    static Kimono<String> getTitleOfAPageFake(String textSoFar) {
        var future = new CompletableFuture<String>();
        writeLog("Created future");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            writeLog("Executing runnable for future");
            try {
                Thread.sleep(1_000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            future.complete("wikipedia");
            executor.shutdown();
        });
        return Kimono.produce(future);
    }

    static Kimono<String> getTitleOfAPage(String textSoFar) {
        var future = new CompletableFuture<String>();
        writeLog("Created future");
        ExecutorService executor = Executors.newFixedThreadPool(1);
        executor.execute(() -> {
            writeLog("Executing runnable for future");
            InputStream response = null;
            try {
                String url = "https://www.wikipedia.org";
                response = new URL(url).openStream();

                Scanner scanner = new Scanner(response);
                String responseBody = scanner.useDelimiter("\\A").next();
                var title = responseBody.substring(responseBody.indexOf("<title>") + 7, responseBody.indexOf("</title>"));

                future.complete(title);

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

    public static String toTitleCase(String input) {
        StringBuilder titleCase = new StringBuilder(input.length());
        boolean nextTitleCase = true;

        for (char c : input.toCharArray()) {
            if (Character.isSpaceChar(c)) {
                nextTitleCase = true;
            } else if (nextTitleCase) {
                c = Character.toTitleCase(c);
                nextTitleCase = false;
            }
            titleCase.append(c);
        }
        return titleCase.toString();
    }

    private static void writeLog(String pattern, Object... value) {
        var prefix = String.format("t:%s c:%s \t", Thread.currentThread().getName(), "MAIN");
        System.out.println(String.format(prefix + pattern, value));
    }

}

