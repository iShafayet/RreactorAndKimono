import rreactor.Kimono;

public class EntryPoint2 {


    public static void main(String[] args) throws InterruptedException {

        // WORKING
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

//        Kimono
//                .now(Math.random())
//                .map(rndNum -> rndNum > 0.5 ? " Good" : " Sad")
//                .sideEffect(str -> {
//                    System.out.println("OUTPUT: " + str);
//                })
//                .run();

        Kimono.now("Life")
                .map(str -> str + " is")
                .chain(str -> {
                    return Kimono
                            .now(Math.random())
                            .map(rndNum -> str + (rndNum > 0.5 ? " Good" : " Sad"));
                })
                .map(str -> str + ".")
                .sideEffect(str -> {
                    System.out.println("OUTPUT: " + str);
                })
                .run();

        Thread.sleep(1_000);

//        Kimono.now("test")
//                .map(str -> 10)
//                .thenKimono(num -> {
//                    return Kimono.later(Integer, 1000);
//                })
//                .thenKimono(s -> {
//                    Kimono.combine(k1, k2)
//                })
//                .onError(s->{
//
//                })
//                .run(s->{
//
//                });
    }


}

