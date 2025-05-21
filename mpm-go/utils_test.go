package main

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestGenerateTOTP(t *testing.T) {
	tests := []struct {
		name     string
		secret   string
		interval int64
		want     string
		wantErr  bool
	}{
		{
			name:     "valid secret and interval",
			secret:   "aaabbbccc",
			interval: 1234567890,
			want:     "712049", // This is an example value, replace with actual expected TOTP
			wantErr:  false,
		},
		{
			name:     "empty secret",
			secret:   "",
			interval: 1234567890,
			want:     "",
			wantErr:  true,
		},
		{
			name:     "zero interval",
			secret:   "JBSWY3DPEHPK3PXP",
			interval: 0,
			want:     "", // Example value for counter=0
			wantErr:  true,
		},
		{
			name:     "negative interval",
			secret:   "JBSWY3DPEHPK3PXP",
			interval: -30,
			want:     "", // Should behave same as zero interval
			wantErr:  true,
		},
	}

	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			got := generateTOTP(tt.secret, tt.interval)
			if tt.wantErr {
				// For error cases, we just check if the result is empty
				// since the function doesn't return errors
				assert.Empty(t, got)
			} else {
				assert.Equal(t, tt.want, got)
			}
		})
	}
}
