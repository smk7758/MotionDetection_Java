package com.github.smk7758.MotionDetection_Java;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;

public class Main {
	public static final String SOFTWARE_NAME = "MotionDetection_Java_0.0.1";

	static {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
	}

	public static void main(String[] args) {
		// ~ <inputfolder> <outputfolder>
		if (args.length < 1) {
			System.out.println("args are too short.");
			return;
		}
		Path inputfolder_path = Paths.get(args[0]);
		Path outputfolder_path = Paths.get(args[1]);
		if (!Files.isDirectory(inputfolder_path)) {
			System.out.println("inputpath is not a folder.");
		} else if (!Files.isDirectory(outputfolder_path)) {
			System.out.println("outputpath is not a folder.");
		}
		List<Path> inputfiles = null;
		try {
			inputfiles = Files.list(inputfolder_path).collect(Collectors.toList());
		} catch (IOException e) {
			e.printStackTrace();
		}
		// inputfiles check
		if (inputfiles == null) {
			System.out.println("cant find the input files.");
			return;
		}
		int inputfiles_size = inputfiles.size();
		if (inputfiles_size == 0) {
			System.out.println("there is any files in the inputfolder.");
		}
		VideoCapture vc = new VideoCapture(); // Like a Stream of Video
		Mat mat_capture = new Mat(); // その時のframe
		Mat mat_first = null; // 一番最初のframe(背景)
		Mat mat_export = new Mat(); // 出力frame
		boolean has_next_frame = true;
		Path outputfile_path = null;
		VideoWriter vw = new VideoWriter();
		for (Path inputfile_path : inputfiles) {
			outputfile_path = Paths.get(outputfolder_path.toString() + FileSystems.getDefault().getSeparator()
					+ inputfile_path.getFileName().toString());// Using new thing from Java8: Path, Files, FileSystems
			vc.open(inputfile_path.toString()); // Open
			vc.read(mat_capture); // Read from vc to mat_capture.
			if (mat_capture.empty()) {
				System.out.println("cant read from video.");
				break;
			}
			mat_first = mat_capture.clone();
			while (has_next_frame) {
				has_next_frame = vc.read(mat_capture); // Re:read
				if (!mat_capture.empty()) {
					// get the diff.
					Core.absdiff(mat_first, mat_capture, mat_export);

					// convent bit size(?)
					mat_first.convertTo(mat_first, CvType.CV_8U);
					mat_export.convertTo(mat_export, CvType.CV_32F);

					// What's this? -> 差分した動体の軌跡が残る程度
					Imgproc.accumulateWeighted(mat_first, mat_export, 0.022);

					// Output video
					int codec = Integer.valueOf("MP4S");
					double fps = 0.0;
					Size size = new Size(1920, 1080);
					vw.open(outputfile_path.toString(), codec, fps, size);
					System.out.println("Output: " + outputfile_path.toString());
				}
			}
			vw.release();
			vc.release();
		}
	}
}
