<?php

namespace App\Models;

use App\Models\Base\Attendance as BaseAttendance;

class Attendance extends BaseAttendance
{
	protected $fillable = [
		'worker_id',
		'date',
		'in_time',
		'out_time',
		'work_hour',
		'over_time',
		'late_time',
		'early_out_time',
		'in_location_id',
		'out_location_id'
	];

    protected $dates = [
        'date'
    ];

    protected $casts = [
        'date'  => 'date:Y-m-d',
    ];
}
