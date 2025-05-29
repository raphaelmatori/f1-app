import { __decorate } from "tslib";
import { Pipe } from '@angular/core';
let DateFormatPipe = class DateFormatPipe {
    transform(dateString) {
        if (!dateString) {
            return 'Invalid Date';
        }
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return 'Invalid Date';
        }
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }
};
DateFormatPipe = __decorate([
    Pipe({
        name: 'dateFormat',
        standalone: true
    })
], DateFormatPipe);
export { DateFormatPipe };
