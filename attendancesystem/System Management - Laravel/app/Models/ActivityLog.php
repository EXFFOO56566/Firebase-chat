<?php

namespace App\Models;

use App\Models\Base\ActivityLog as BaseActivityLog;

class ActivityLog extends BaseActivityLog
{
	protected $fillable = [
		'log_name',
		'description',
		'subject_type',
		'subject_id',
		'causer_type',
		'causer_id',
		'properties'
	];
}
