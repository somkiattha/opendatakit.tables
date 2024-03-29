/*
 * Copyright (C) 2012 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.tables.utils;

import android.util.Log;

/**
 * This is a general place for utils regarding odktables files. These are files
 * that are associated with various tables, such as html files for different
 * views, etc.
 * @author sudar.sam@gmail.com
 *
 */
public class TableFileUtils {

  private static final String TAG = TableFileUtils.class.getSimpleName();

  /** The default app name for ODK Tables */
  private static final String ODK_TABLES_APP_NAME = "tables";

  public static final String getDefaultAppName() {
    Log.i(TAG, "appName is null on intent");
    Thread.dumpStack();
    return ODK_TABLES_APP_NAME;
  }
}
