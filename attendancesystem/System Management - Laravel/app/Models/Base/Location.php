<?php

/**
 * Created by Reliese Model.
 */

namespace App\Models\Base;

use Illuminate\Database\Eloquent\Model;

/**
 * Class Location
 * 
 * @property int $id
 * @property float $lat
 * @property float $longt
 * @property int $area_id
 *
 * @package App\Models\Base
 */
class Location extends Model
{
	protected $table = 'locations';
	public $timestamps = false;

	protected $casts = [
		'lat' => 'float',
		'longt' => 'float',
		'area_id' => 'int'
	];
}
