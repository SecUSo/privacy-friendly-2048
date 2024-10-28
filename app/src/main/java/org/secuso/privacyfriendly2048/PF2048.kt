/*
 This file is part of Privacy Friendly 2048.

 Privacy Friendly Sudoku is free software:
 you can redistribute it and/or modify it under the terms of the
 GNU General Public License as published by the Free Software Foundation,
 either version 3 of the License, or any later version.

 Privacy Friendly Sudoku is distributed in the hope
 that it will be useful, but WITHOUT ANY WARRANTY; without even
 the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 See the GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Privacy Friendly Sudoku. If not, see <http://www.gnu.org/licenses/>.
 */
package org.secuso.privacyfriendly2048

import androidx.core.content.ContextCompat
import org.secuso.pfacore.ui.PFApplication
import org.secuso.pfacore.ui.PFData
import org.secuso.privacyfriendly2048.activities.MainActivity

class PF2048 : PFApplication() {
    override val name: String
        get() = ContextCompat.getString(this, R.string.app_name_long)
    override val data: PFData
        get() = PFApplicationData.instance(this).data
    override val mainActivity = MainActivity::class.java
}
