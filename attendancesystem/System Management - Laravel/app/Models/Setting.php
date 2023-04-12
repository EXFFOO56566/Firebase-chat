<?php

namespace App\Models;

use App\Models\Base\Setting as BaseSetting;

class Setting extends BaseSetting
{
	protected $fillable = [
		'start_time',
		'out_time',
		'key_app',
        'timezone'
    ];
}
