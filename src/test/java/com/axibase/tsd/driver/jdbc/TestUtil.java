package com.axibase.tsd.driver.jdbc;

import java.io.*;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipInputStream;

import com.axibase.tsd.driver.jdbc.content.ContentMetadata;
import com.axibase.tsd.driver.jdbc.enums.AtsdType;
import com.axibase.tsd.driver.jdbc.util.EnumUtil;
import org.apache.calcite.avatica.ColumnMetaData;

import static com.axibase.tsd.driver.jdbc.content.ContentMetadata.getAvaticaType;

public class TestUtil {
	private TestUtil() {
	}

	public static String resourceToString(String relativePath, Class<?> clazz) {
		DataInputStream dataIs = null;
		try {
			final URL url = clazz.getResource(relativePath);
			if (url == null) {
				throw new IOException("File not found by given relative path");
			}
			final File path = new File(url.toURI());

			byte[] fileAsBytes = new byte[(int)path.length()];
			dataIs = new DataInputStream(new FileInputStream(path));
			dataIs.readFully(fileAsBytes);
			return new String(fileAsBytes, DriverConstants.DEFAULT_CHARSET);
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (dataIs != null) {
				try {
					dataIs.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static InputStream getInputStreamForResource(String relativePathToResource, Class<?> clazz) throws IOException {
		final InputStream mockIs = clazz.getResourceAsStream(relativePathToResource);
		if (relativePathToResource.endsWith(".zip")) {
			ZipInputStream mockZip = new ZipInputStream(mockIs);
			mockZip.getNextEntry();
			return mockZip;
		}
		return mockIs;
	}

	public static List<ColumnMetaData> prepareMetadata(String relativePathToResource, Class<?> clazz) {
		InputStream inputStream = null;
		BufferedReader reader = null;
		try {
			inputStream = getInputStreamForResource(relativePathToResource, clazz);
			reader = new BufferedReader(new InputStreamReader(inputStream));
			return prepareMetadata(reader.readLine());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (IOException ioe) {
				// do nothing
			}
		}
	}

	public static List<ColumnMetaData> prepareMetadata(String header) {
		if (header.startsWith("#")) {
			return Collections.singletonList(ColumnMetaData.dummy(getAvaticaType(AtsdType.STRING_DATA_TYPE), false));
		} else {
			String[] columnNames = header.split(",");
			ColumnMetaData[] meta = new ColumnMetaData[columnNames.length];
			for (int i = 0; i < columnNames.length; i++) {
				final String columnName = columnNames[i];
				meta[i] = new ContentMetadata.ColumnMetaDataBuilder()
						.withName(columnName)
						.withTitle(columnName)
						.withColumnIndex(i)
						.withNullable(columnName.startsWith("tag") ? 1 : 0)
						.withAtsdType(EnumUtil.getAtsdTypeByColumnName(columnName))
						.build();
			}
			return Arrays.asList(meta);
		}
	}
}
