package copel.affiliateproductpackage.adult.unit;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class Video {
    /**
     * 動画ファイルの中身.
     */
    private byte[] content;

    /**
     * 指定した秒～秒までの間でビデオをカットする.
     *
     * @param start 開始位置
     * @param end 終了位置
     * @return カット成功すればtrue、それ以外はfalse
     */
    public boolean cut(final int start, final int end) {
        if (this.content == null || this.content.length == 0) return false;

        try {
            // 一時ファイルとして保存
            Path tempInput = Files.createTempFile("temp_video_input_", ".mp4");
            Path tempOutput = Files.createTempFile("temp_video_cut_", ".mp4");
            Files.write(tempInput, this.content);

            // ffmpegコマンドを組み立て
            String[] command = {
                "ffmpeg",
                "-y",
                "-ss", String.valueOf(start),
                "-to", String.valueOf(end),
                "-i", tempInput.toAbsolutePath().toString(),
                "-c", "copy",
                tempOutput.toAbsolutePath().toString()
            };

            // 実行
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();

            // 出力ログを捨てるか、ログファイルに残したければここで処理
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                while (reader.readLine() != null) {}
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("ffmpegエラー: exit code: {}", exitCode);
                return false;
            }

            // カットされた動画を読み込む
            this.content = Files.readAllBytes(tempOutput);
            return true;

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定したファイルパスにこの動画を保存する.
     *
     * @param filePath ファイルパス
     * @return 保存成功すればtrue、それ以外はfalse
     */
    public boolean save(final String filePath) {
        if (this.content == null || this.content.length == 0) return false;
        try {
            File file = new File(filePath);
            File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                if (!parent.mkdirs()) {
                    log.error("ディレクトリ作成に失敗: {}", parent.getAbsolutePath());
                    return false;
                }
            }
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(this.content);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定したファイルパス先の動画を読み込みこのクラスに持つ.
     *
     * @param filePath ファイルパス
     * @return 読み込み成功すればtrue、それ以外はfalse
     */
    public boolean read(final String filePath) {
        try {
            content = Files.readAllBytes(Paths.get(filePath));
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定したURL先の動画をダウンロードして読み込み、このクラスに持つ.
     *
     * @param urlStr URL
     * @return 読み込み成功すればtrue、それ以外はfalse
     */
    public boolean downloadAndRead(final String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            try (InputStream is = conn.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                this.content = baos.toByteArray();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
