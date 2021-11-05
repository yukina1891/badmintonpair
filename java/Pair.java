import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Pair {
	int n, d, g, n_max, e[][], pair[][][];

	public Pair(int n, int d, int g) {
		this.n = n; // 1チームの人数
		this.d = d; // 1試合dダブ
		this.g = g; // 試合数
		this.n_max = 2 * d; // ダブりがなしで組める人数
		this.e = new int[this.n_max][this.n_max]; // ペアを組んでない場合0、組んだ場合1
		this.pair = new int[g][d][2]; // 組み方

		for (int i = 0; i < this.n_max; i++) {
			for (int j = 0; j < this.n_max; j++) {
				if (i == j) {
					this.e[i][j] = 1;
				} else {
					this.e[i][j] = 0;
				}
			}
		}
	}

	public void makePair() { // 組み合わせを作る関数
		boolean b;

		int[] tmp1 = new int[n_max];
		for (int i = 0; i < n_max; i++) {
			tmp1[i] = 0;
		}

		int[][] tmp2 = new int[this.g][this.n];
		for (int i = 0; i < this.g; i++) {
			for (int j = 0; j < this.n; j++) {
				tmp2[i][j] = 0;
			}
		}

		for (int i = 0; i < this.g; i++) {
			b = this.build(i, 0, 0, 0, tmp1);
			if (b == false) {
				System.out.println("error：build");
				System.exit(0);
			}
		}

		b = this.modify(0, 0, -1, -1, tmp1, tmp2, this.e, -1);
		if (b == false) {
			System.out.println("error：modify");
			System.exit(0);
		}
	}

	public boolean build(int g, int d, int k, int l, int t[]) { // g試合目、dダブ目、kとlを組ませた、tは直前のtmp
		int i;
		boolean b;

		int[] tmp = new int[n_max];
		for (i = 0; i < n_max; i++) {
			tmp[i] = t[i];
		}

		if (d != 0) { // tmp[]を更新（最初は0のままにしておく）
			tmp[k] = 1;
			tmp[l] = 1;
		}

		i = 0;
		while (tmp[i] == 1) { // tmp[]の値が全て1ならばtrueを返す
			if (i == n_max - 1) {
				return true;
			}
			i++;
		}

		for (int p = 0; p < n_max; p++) {
			if (tmp[p] == 1) {
				continue;
			}
			for (int q = 0; q < n_max; q++) {
				if (tmp[q] == 1 || e[p][q] == 1) {
					continue;
				}
				b = this.build(g, d + 1, p, q, tmp);
				if (b) {
					pair[g][d][0] = p;
					pair[g][d][1] = q;
					e[p][q] = 1;
					e[q][p] = 1;
					return true;
				}
			}
		}

		return false;
	}

	public boolean modify(int g, int d, int k, int l, int t1[], int t2[][], int t_e[][], int sw) { // g試合目のdダブ目について、直前にkとlを組ませた、swはpとq片方変えた場合は0、両方の場合は1
		if (g == this.g && d == 0) {
			return true;
		}

		int next_g, next_d;
		if (d == this.d - 1) { // 次に再帰するときの値を代入
			next_g = g + 1;
			next_d = 0;
		} else {
			next_g = g;
			next_d = d + 1;
		}

		int[] tmp1 = new int[this.n]; // 全試合通してのダブり回数に差が出ないようにするための配列
		int[][] tmp2 = new int[this.g][this.n]; // g試合目に同じ人がダブらないようにするための配列
		int[][] tmp_e = new int[this.n][this.n];

		for (int i = 0; i < this.n; i++) {
			tmp1[i] = t1[i];
		}
		for (int i = 0; i < this.g; i++) {
			for (int j = 0; j < this.n; j++) {
				tmp2[i][j] = t2[i][j];
			}
		}
		for (int i = 0; i < this.n; i++) {
			for (int j = 0; j < this.n; j++) {
				tmp_e[i][j] = t_e[i][j];
			}
		}

		if (k >= 0 && l >= 0) { // tmpを更新
			if (sw == 1) { // kとlの両方を入れ替えたとき
				if (d == 0) {
					tmp2[g - 1][k] = 1;
				} else {
					tmp2[g][k] = 1;
				}
				tmp1[k] = 1;
			}
			if (d == 0) {
				tmp2[g - 1][l] = 1;
			} else {
				tmp2[g][l] = 1;
			}
			tmp1[l] = 1;

			tmp_e[k][l] = 1;
			tmp_e[l][k] = 1;
		}

		tmp1 = this.check_tmp1(tmp1); // 全て1になったら0に戻す

		boolean b;

		if (pair[g][d][0] < this.n && pair[g][d][1] < this.n) { // ペアを置き換えなくて良いとき
			return this.modify(next_g, next_d, -1, -1, tmp1, tmp2, tmp_e, -1);

		} else if (pair[g][d][0] >= this.n && pair[g][d][1] >= this.n) { // ペアの両方を置き換えるとき
			for (int p = 0; p < this.n; p++) {
				if (tmp1[p] == 1 || tmp2[g][p] == 1) {
					continue;
				}
				for (int q = 0; q < this.n; q++) {
					if (tmp1[q] == 1 || tmp2[g][q] == 1 || tmp_e[p][q] == 1) {
						continue;
					}
					b = this.modify(next_g, next_d, p, q, tmp1, tmp2, tmp_e, 1);
					if (b) {
						pair[g][d][0] = p;
						pair[g][d][1] = q;
						e[p][q] = 1;
						e[q][p] = 1;
						return true;
					}
				}
			}

		} else { // ペアの片方を置き換えるとき
			int p = pair[g][d][0];

			for (int q = 0; q < this.n; q++) {
				if (tmp1[q] == 1 || tmp2[g][q] == 1 || tmp_e[p][q] == 1) {
					continue;
				}
				b = this.modify(next_g, next_d, p, q, tmp1, tmp2, tmp_e, 0);
				if (b) {
					if (p < q) {
						pair[g][d][0] = p;
						pair[g][d][1] = q;
					} else {
						pair[g][d][0] = q;
						pair[g][d][1] = p;
					}
					e[p][q] = 1;
					e[q][p] = 1;
					return true;
				}
			}
		}

		return false;
	}

	public int[] check_tmp1(int[] t) { // tの要素が全て1のとき全てを0に戻す
		for (int i = 0; i < this.n; i++) {
			if (t[i] == 0) {
				return t;
			}
		}
		for (int i = 0; i < this.n; i++) {
			t[i] = 0;
		}
		return t;
	}

	public void printPair() {
		int len1, len2, len_max, sp1, sp2;
		for (int i = 0; i < d; i++) {
			for (int j = 0; j < g; j++) {
				len1 = String.valueOf(pair[j][i][0] + 1).length();
				len2 = String.valueOf(pair[j][i][1] + 1).length();
				len_max = String.valueOf(this.n).length();
				sp1 = len_max - len1;
				sp2 = len_max - len2;
				for (int k = 0; k < sp1; k++) {
					System.out.print(" ");
				}
				System.out.print((pair[j][i][0] + 1) + "-" + (pair[j][i][1] + 1) + "  ");
				for (int l = 0; l < sp2; l++) {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		System.out.println();
	}

	public void printGame_num() {
		int[] num = new int[this.n];

		for (int i = 0; i < this.n; i++) {
			for (int j = i + 1; j < this.n; j++) {
				if (e[i][j] == 1) {
					num[i]++;
					num[j]++;
				}
			}
		}

		System.out.println("試合数");
		for (int i = 0; i < n; i++) {
			System.out.print((i + 1) + "：" + num[i] + "  ");
		}
		System.out.println();
	}

	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print("チームの人数：");
			String s1 = br.readLine();
			System.out.print("1試合のダブルス数：");
			String s2 = br.readLine();
			System.out.print("試合数：");
			String s3 = br.readLine();

			int i1 = Integer.parseInt(s1);
			int i2 = Integer.parseInt(s2);
			int i3 = Integer.parseInt(s3);

			if ((i1 + 1) / 2 > i2) { // 試合ごとに出場しない人が出てしまうとき
				System.out.println("error : 試合ごとに出場しない人が出てしまいます");
				System.exit(0);
			} else if ((i1 - 1) * i1 / 2 < i2 * i3) { // 同じ組み合わせができてしまうとき
				System.out.println("error : 同じ組み合わせができてしまいます");
				System.exit(0);
			}

			Pair p = new Pair(i1, i2, i3);
			System.out.println();
			p.makePair();
			p.printPair();
			p.printGame_num();

		} catch (IOException e) {
			System.out.println("error");
		}
	}

}
