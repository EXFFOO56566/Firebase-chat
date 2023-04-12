<?php

/**
 * Created by Reliese Model.
 */

namespace App\Models\Base;

use App\Models\Attendance;
use Carbon\Carbon;
use Illuminate\Database\Eloquent\Collection;
use Illuminate\Database\Eloquent\Model;

/**
 * Class Area
 * 
 * @property int $id
 * @property string $name
 * @property string $address
 * @property Carbon $created_at
 * @property Carbon $updated_at
 * 
 * @property Collection|Attendance[] $attendances
 *
 * @package App\Models\Base
 */
class Area extends Model
{
	protected $table = 'areas';

	public function attendances()
	{
		return $this->hasMany(Attendance::class, 'out_location_id');
	}
}
