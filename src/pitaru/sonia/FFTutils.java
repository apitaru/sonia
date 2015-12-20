package pitaru.sonia;

class FFTutils {

	float HALF_PI = (float) Math.PI / 2.0f;
	float TWO_PI = (float) Math.PI * 2.0f;

	int WINDOW_SIZE, WS2;
	int BIT_LEN;
	int[] _bitrevtable;
	float _normF;
	float[] _equalize;
	float[] _envelope;
	float[] _fft_result;
	float[][] _fftBuffer;
	float[] _cosLUT, _sinLUT;
	float[] _iirfX, _iirfY;
	boolean _isEqualized, _hasEnvelope;

	public FFTutils(int windowSize) {
		WINDOW_SIZE = WS2 = windowSize;
		WS2 >>= 1;
		BIT_LEN = (int) (Math.log(WINDOW_SIZE) / 0.693147180559945 + 0.5);
		_normF = 2f / WINDOW_SIZE;
		_hasEnvelope = false;
		_isEqualized = false;
		initFFTtables();
	}

	void initFFTtables() {
		_cosLUT = new float[BIT_LEN];
		_sinLUT = new float[BIT_LEN];
		_fftBuffer = new float[WINDOW_SIZE][2];
		_fft_result = new float[WS2];

		// only need to compute sin/cos at BIT_LEN angles
		float phi = (float) Math.PI;
		for (int i = 0; i < BIT_LEN; i++) {
			_cosLUT[i] = (float) Math.cos(phi);
			_sinLUT[i] = (float) Math.sin(phi);
			phi *= 0.5;
		}

		// precalc bit reversal lookup table ala nullsoft
		int i, j, bitm, temp;
		_bitrevtable = new int[WINDOW_SIZE];

		for (i = 0; i < WINDOW_SIZE; i++)
			_bitrevtable[i] = i;
		for (i = 0, j = 0; i < WINDOW_SIZE; i++) {
			if (j > i) {
				temp = _bitrevtable[i];
				_bitrevtable[i] = _bitrevtable[j];
				_bitrevtable[j] = temp;
			}
			bitm = WS2;
			while (bitm >= 1 && j >= bitm) {
				j -= bitm;
				bitm >>= 1;
			}
			j += bitm;
		}
	}

	// taken from nullsoft VMS
	void useEqualizer(boolean on) {
		_isEqualized = on;
		if (on) {
			int i;
			float scaling = -0.02f;
			float inv_half_nfreq = 1.0f / WS2;
			_equalize = new float[WS2];
			for (i = 0; i < WS2; i++)
				_equalize[i] = scaling
						* (float) Math.log((double) (WS2 - i) * inv_half_nfreq);
		}
	}

	void useEnvelope(boolean on, float power) {
		_hasEnvelope = on;
		if (on) {
			int i;
			float mult = 1.0f / (float) WINDOW_SIZE * TWO_PI;
			_envelope = new float[WINDOW_SIZE];
			if (power == 1.0f) {
				for (i = 0; i < WINDOW_SIZE; i++)
					_envelope[i] = 0.5f + 0.5f * (float) Math.sin(i * mult - HALF_PI);
			} else {
				for (i = 0; i < WINDOW_SIZE; i++)
					_envelope[i] = (float) Math.pow(
							0.5f + 0.5f * (float) Math.sin(i * mult - HALF_PI), power);
			}
		}
	}

	float[] computeFFT(float[] waveInData) {
		float u_r, u_i, w_r, w_i, t_r, t_i;
		int l, le, le2, j, jj, ip, ip1, i, ii, phi;

		// check if we need to apply window function or not
		if (_hasEnvelope) {
			for (i = 0; i < WINDOW_SIZE; i++) {
				int idx = _bitrevtable[i];
				if (idx < WINDOW_SIZE)
					_fftBuffer[i][0] = waveInData[idx] * _envelope[idx];
				else
					_fftBuffer[i][0] = 0;
				_fftBuffer[i][1] = 0;
			}
		} else {
			for (i = 0; i < WINDOW_SIZE; i++) {
				int idx = _bitrevtable[i];
				if (idx < WINDOW_SIZE)
					_fftBuffer[i][0] = waveInData[idx];
				else
					_fftBuffer[i][0] = 0;
				_fftBuffer[i][1] = 0;
			}
		}

		for (l = 1, le = 2, phi = 0; l <= BIT_LEN; l++) {
			le2 = le >> 1;
			w_r = _cosLUT[phi];
			w_i = _sinLUT[phi++];
			u_r = 1f;
			u_i = 0f;
			for (j = 1; j <= le2; j++) {
				for (i = j; i <= WINDOW_SIZE; i += le) {
					ip = i + le2;
					ip1 = ip - 1;
					ii = i - 1;
					t_r = _fftBuffer[ip1][0] * u_r - u_i * _fftBuffer[ip1][1];
					t_i = _fftBuffer[ip1][1] * u_r + u_i * _fftBuffer[ip1][0];
					_fftBuffer[ip1][0] = _fftBuffer[ii][0] - t_r;
					_fftBuffer[ip1][1] = _fftBuffer[ii][1] - t_i;
					_fftBuffer[ii][0] += t_r;
					_fftBuffer[ii][1] += t_i;
				}
				t_r = u_r * w_r - w_i * u_i;
				u_i = w_r * u_i + w_i * u_r;
				u_r = t_r;
			}
			le <<= 1;
		}
		// normalize bands or apply EQ
		if (_isEqualized) {
			for (i = 0; i < WS2; i++)
				_fft_result[i] = _equalize[i]
						* (float) Math.sqrt(_fftBuffer[i][0] * _fftBuffer[i][0]
								+ _fftBuffer[i][1] * _fftBuffer[i][1]);
		} else {
			for (i = 0; i < WS2; i++)
				_fft_result[i] = _normF
						* (float) Math.sqrt(_fftBuffer[i][0] * _fftBuffer[i][0]
								+ _fftBuffer[i][1] * _fftBuffer[i][1]);
		}
		return _fft_result;
	}

	// IIR LP filter setup
	void initFilter(float cutoff, int len) {
		_iirfX = new float[len + 1];
		_iirfY = new float[len];
		_iirfX[0] = 1 - cutoff;
		_iirfY[0] = cutoff;
	}

	float[] applyFilter(float[] input) {
		float[] output = new float[input.length];
		for (int i = _iirfY.length; i < input.length; i++) {
			float sum = input[i] * _iirfX[0];
			for (int j = 0; j < _iirfY.length; j++) {
				int tmp = j + 1;
				sum += input[i - tmp] * _iirfX[tmp] + output[i - tmp] * _iirfY[j];
			}
			output[i] = sum;
		}
		return output;
	}
}