package prob;

public class Binomial {

    public static long binomial(int n, int k){
        if(n < k)
            return -1;

        long den,nom;
        if((n-k) > k){
            nom = partialFactorial(n,n-k+1);
            den = factorial(k);

        }else{
            nom = partialFactorial(n,k);
            den = factorial(n-k);
        }

        return nom/den;
    }

    public static long factorial(int n){
        return partialFactorial(n,1);
    }

    public static long partialFactorial(int n, int k){ //k from where to start
        long fact = k;
        for (int i = k+1; i <= n; i++) {
            fact*=i;
        }
        return fact;
    }

}
