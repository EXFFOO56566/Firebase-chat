<?php

/**
 * Created by Reliese Model.
 */

namespace App\Models\Base;

use Carbon\Carbon;
use Illuminate\Database\Eloquent\Model;

/**
 * Class ActivityLog
 * 
 * @property int $id
 * @property string $log_name
 * @property string $description
 * @property string $subject_type
 * @property int $subject_id
 * @property string $causer_type
 * @property int $causer_id
 * @property string $properties
 * @property Carbon $created_at
 * @property Carbon $updated_at
 *
 * @package App\Models\Base
 */
class ActivityLog extends Model
{
	protected $table = 'activity_log';

	protected $casts = [
		'subject_id' => 'int',
		'causer_id' => 'int'
	];
}
