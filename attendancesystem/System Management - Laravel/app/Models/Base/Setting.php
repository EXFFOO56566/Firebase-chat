<?php

/**
 * Created by Reliese Model.
 */

namespace App\Models\Base;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Model;

/**
 * Class Setting
 *
 * @property int $id
 * @property Carbon $start_time
 * @property Carbon $out_time
 * @property string $key_app
 * @property string $timezone
 * @property Carbon $created_at
 * @property Carbon $updated_at
 *
 * @package App\Models\Base
 */
class Setting extends Model
{
	protected $table = 'settings';
}
