import java.lang.Math;

public class ExponentialRandom
{
    public int nextInt()
    {
        double lambda = 0.001;
        double r = Math.random();
        double x;

        while(true)
        {
            x = -Math.log(r) / lambda; //.math libraries
            if(x > 8000)
            {
                continue;
            }
            break;
        }

        return (int)Math.floor(x);
    }
}