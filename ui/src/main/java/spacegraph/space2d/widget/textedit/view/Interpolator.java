package spacegraph.space2d.widget.textedit.view;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jcog.TODO;

import java.util.concurrent.ExecutionException;

public enum Interpolator {

  LINEAR {
    @Override
    protected double[] newCurve(int divOfNum) {
      double[] result = new double[divOfNum];
        double gain = 1.0 / divOfNum;
        for (int i = 0; i < divOfNum; i++) {
        result[i] = gain;
        }
        return result;
    }
  },
  SMOOTH {
    @Override
    protected double[] newCurve(int divOfNum) {
      double[] result = new double[divOfNum];
      double gain = 1.0 / divOfNum;
      double g = Math.PI * gain;
      for (int i = 0; i < divOfNum; i++) {
        double divGain = Math.cos(g * i) - Math.cos(g * (i + 1));
        result[i] = (divGain / 2);
      }
      return result;
    }
  },
  SMOOTH_OUT {
    @Override
    protected double[] newCurve(int divOfNum) {
      double[] result = new double[divOfNum];
      double gain = 1.0 / divOfNum;
      double g = Math.PI * gain / 2.0;
      for (int i = 0; i < divOfNum; i++) {
        double divGain = Math.sin(g * (i + 1)) - Math.sin(g * i);
        result[i] = (divGain);
      }
      return result;
    }
  },
  SMOOTH_IN {
    @Override
    protected double[] newCurve(int divOfNum) {
      double[] result = new double[divOfNum];
      double gain = 1.0 / divOfNum;
      double g = Math.PI * gain / 2.0;
      double start = Math.PI * 1.5;
      for (int i = 0; i < divOfNum; i++) {
        double divGain = Math.sin(start + (g * (i + 1))) - Math.sin(start + (g * i));
        result[i] = (divGain);
      }
      return result;
    }
  },
  BOUND {
    @Override
    protected double[] newCurve(int divOfNum) {
      double[] result = new double[divOfNum];
      double gain = 1.0 / divOfNum;
      double g = Math.PI * 1.5 * gain;
      double qg = Math.PI / 4.0;
      double dd = Math.sin(qg) * 2;
      for (int i = 0; i < divOfNum; i++) {
        result[i] = ((Math.sin(jcog.Util.fma(g, i, qg)) - Math.sin(jcog.Util.fma(g, (i + 1), qg))) / dd);
      }
      return result;
    }
  };

  @Deprecated private final LoadingCache<Integer, double[]> cache =
      CacheBuilder.newBuilder().maximumSize(1000).build(new CacheLoader<>() {
        @Override
        public double[] load(Integer divOfNum) {
          return newCurve(divOfNum);
        }
      });

  /** point sample: x in range 0..1, y in range 0..1 */
  public static float get(float x) {
      throw new TODO();
  }

  protected abstract double[] newCurve(int divOfNum);

  public double[] curve(int divOfNum) {
    try {
      return cache.get(divOfNum);
    } catch (ExecutionException e) {
      throw new RuntimeException(e);
    }
  }
}